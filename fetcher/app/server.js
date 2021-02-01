'use strict';
const amqp = require('amqplib');
const kafkajs = require('kafkajs')
const PromiseQueue = require("promise-queue");
const crypto = require('crypto');
const avro = require('avro-js');
const conf = require('./config.js');
const browser = require('./browser.js')
const storage = require('./storage.js')


process.stdin.resume();

/* Global variables */
let connection = null;
let browserObject = null;
let context = null;


const maxConcurrent = conf.PLAYWRIGHT_TAB_LIMIT;
const maxQueue = conf.PLAYWRIGHT_TAB_LIMIT;
const queue = new PromiseQueue(maxConcurrent, maxQueue);

const type = avro.parse({
  name: "URLResponse",
  type: "record",
  fields: [
    {name: "status", type: "int"},
    {name: "domain", type: "string"},
    {name: "url", type: "string"},
    {name: "queue", type: "string"},
    {name: "html", type: "string"}
  ]
});

(async () => {
  browserObject = await browser.launchBrowser(conf.PLAYWRIGHT_BROWSER);
  context = await browser.createContext(browserObject);
  console.log(`Connecting to ${conf.RABBITMQ_CONNECTION_URL}`);

  const kafkaClient = new kafkajs.Kafka({
    clientId: conf.KAFKA_CLIENT_ID,
    brokers: conf.KAFKA_BROKERS
  });  
  const kafkaProducer = kafkaClient.producer();
  await kafkaProducer.connect();

  connection = await amqp.connect(conf.RABBITMQ_CONNECTION_URL);
  const ch = await connection.createChannel();
  await ch.assertQueue(conf.RABBITMQ_QUEUE_NAME, {durable: true});

  function processMessage (msg) {
    let url = msg.content.toString();

    // TODO: temporarily?
    if (!url.startsWith("http")) {
      url = `https://${url}`;
    }

    const urlHash = crypto.createHash('md5').update(url).digest("hex");
    queue.add(async function() {
      console.log(`An URL / (${url}) added to the PromiseQueue`);
      try {
        const response = await browser.open(context, url);
          console.log("saving an AVRO file to kafka");
          try {
            const domain = (new URL(url)).hostname;
            // console.log(`domain: ${domain}`)
            const value = type.toBuffer({
              status: parseInt(response.statusCode),
              domain: domain,  // TODO: domain should be provided
              url: url,
              queue: "frontier",
              html: response.htmlContent
            });
            await kafkaProducer.send({
              topic: conf.KAFKA_TOPIC,
              messages: [
                {key: urlHash, value: value, partition: 0}
              ],
            });
          } catch(e) {
            console.log("kafka");
            console.log(e);
          }

        if (response.screenshotBuffer != null) {
          console.log("saving a screenshot (stream) to minio (png)");
          const screenshotFileName = `${urlHash}.png`;
          try {
            const etag = await storage.putObjectAsync(conf.MINIO_SCREENSHOT_BUCKET, screenshotFileName, response.screenshotBuffer);
          } catch(e) {
            console.log(e);
          }
        }

      } catch (e) {
        console.log(e);
      }

      console.log("removing message from the RMQ queue...");
      ch.ack(msg);
      return true;
    }).catch(function(reason) {
      ch.nack(msg);
    }, function(reason) {
    })
  }

  console.log(`starting consuming messages from the RMQ queue ${conf.RABBITMQ_QUEUE_NAME}...`);
  ch.consume(conf.RABBITMQ_QUEUE_NAME, processMessage, { noAck: false });

})();


function exitHandler(options, exitCode) {
  if (options.cleanup) {
    console.log("Closing the RMQ connection");
    connection.close();
    console.log("Closing the playwright's context");
    context.close();
    console.log("Closing the playwright's browser");
    browserObject.close();
  }
  if (exitCode || exitCode === 0) console.log(`EXIT CODE: ${exitCode}`);
  if (options.exit) process.exit();
}


// do something when app is closing
process.on('exit', exitHandler.bind(null,{cleanup:true}));

// catches ctrl+c event
process.on('SIGINT', exitHandler.bind(null, {exit:true}));
// process.on('SIGTERM', exitHandler.bind(null, {exit:true}));

// catches "kill pid" (for example: nodemon restart)
process.on('SIGUSR1', exitHandler.bind(null, {exit:true}));
process.on('SIGUSR2', exitHandler.bind(null, {exit:true}));

// catches uncaught exceptions
process.on('uncaughtException', exitHandler.bind(null, {exit:true}));
