'use strict';

const Minio = require('minio');
const conf = require('./config.js');


const client = new Minio.Client({
    endPoint: conf.MINIO_HOST,
    port: conf.MINIO_PORT,
    useSSL: conf.MINIO_USE_SSL,
    accessKey: conf.MINIO_ACCESS_KEY,
    secretKey: conf.MINIO_SECRET_KEY
});


// the promissification of the Minio.putObject method
const putObjectAsync = function(bucketName, objectName, stream) {
  return new Promise((resolve, reject) => {
    client.putObject(bucketName, objectName, stream, stream.length.toString(), function(err, etag) {
      if (err) {
        reject(err);
      } else {
        resolve(etag);
      }
    });
  })
}


exports.client = client;
exports.putObjectAsync = putObjectAsync;
