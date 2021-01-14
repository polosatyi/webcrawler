'use strict';
const playwright = require('playwright');
const conf = require('./config.js');


const browserTypes = ['chromium', 'firefox', 'webkit'];


exports.launchBrowser = async function (browserType) {
  if (browserTypes.includes(browserType)) {
    const br = playwright[browserType];
    console.log(`Launching a browser: ${browserType}`);
    return await br.launch();
  } else {
    throw new Error('invalid browserType');
  }
}


exports.createContext = async function (browser) {
  console.log("Creating a context");
  let options = {};
  if (conf.PLAYWRIGHT_DEVICE_TYPE != null) {
    if (playwright.devices.includes(conf.PLAYWRIGHT_DEVICE_TYPE)) {
      options = playwright.devices[conf.PLAYWRIGHT_DEVICE_TYPE];
    } else {
      throw new Error('invalid browserType');
    }
  } else {
    if (conf.PLAYWRIGHT_USER_AGENT != null) {
      options["userAgent"] = conf.PLAYWRIGHT_USER_AGENT;
    }
    if (conf.PLAYWRIGHT_IS_MOBILE) {
      options["isMobile"] = true;
    }
    if ((conf.PLAYWRIGHT_SCREEN_RESOLUTION_WIDTH != null) && (conf.PLAYWRIGHT_SCREEN_RESOLUTION_WIDTH != null)) {
      options["viewport"] = {
        "width": conf.PLAYWRIGHT_SCREEN_RESOLUTION_WIDTH,
        "height": conf.PLAYWRIGHT_SCREEN_RESOLUTION_WIDTH,
      }
    }  
  }
  return await browser.newContext(options);
}


exports.open = async function(context, url) {
    console.log(`openning an URL: ${url}`);
    const page = await context.newPage();
    try {
      const response = await page.goto(url, {
        "timeout": conf.PLAYWRIGHT_PAGE_TIMEOUT,
        "waitUntil": "load"
      });
    } catch (error) {
      console.log("closing the page");
      await page.close();
      return {
        "statusCode": 604,
        "htmlContent": null,
        "screenshotBuffer": null,
        "metaData": null
      }
    }

    const htmlContent = conf.PLAYWRIGHT_CONTENT_ENABLED ? await page.content() : null;
    const screenshotBuffer = conf.PLAYWRIGHT_SCEENSHOTS_ENABLED ? await page.screenshot({}) : null;
    let metaData = null;

    if (conf.PLAYWRIGHT_METADATA_ENABLED) {
      metaData = {
        "url": url,
        "statusCode": response.status()
      };
    }
      
    console.log("the page is fetched");

    await page.close();
    console.log("closing the page");
    return {
      "statusCode": 200,
      "htmlContent": htmlContent,
      "screenshotBuffer": screenshotBuffer,
      "metaData": metaData
    };
};
