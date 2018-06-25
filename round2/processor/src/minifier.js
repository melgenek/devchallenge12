'use strict';

const puppeteer = require('puppeteer');
const axios = require('axios');
const minimalcss = require('minimalcss');


const PUPPETEER_HOST = process.env.PUPPETEER_HOST || 'localhost';
const PUPPETEER_PORT = process.env.PUPPETEER_PORT || 9223;

class Minifier {

    static async minifyCss(url) {
        let browser;
        try {
            const browserWSEndpoint = await Minifier.debuggerUri();
            browser = await puppeteer.connect({browserWSEndpoint: browserWSEndpoint});
            if (await Minifier._isRedirect(browser, url)) {
                return {
                    redirect: true
                }
            } else {
                const cssResult = await minimalcss.minimize({
                    urls: [url],
                    cssoOptions: {
                        comments: false
                    },
                    browser: browser
                });
                return {
                    result: cssResult.finalCss
                }
            }
        } catch (error) {
            console.error('Failed to minimize CSS: ', error);
            throw error;
        } finally {
            await Minifier._cleanBrowser(browser);
        }
    }

    //https://github.com/GoogleChrome/puppeteer/issues/2242
    //https://chromium-review.googlesource.com/c/chromium/src/+/952522
    static async debuggerUri() {
        const remote = await axios.get(`http://${PUPPETEER_HOST}:${PUPPETEER_PORT}/json/version`, {headers: {Host: 'localhost'}});
        const socketUri = remote.data.webSocketDebuggerUrl.replace('localhost', `${PUPPETEER_HOST}:${PUPPETEER_PORT}`);
        console.log('Chrome uri: ' + socketUri);
        return socketUri;
    }

    // https://github.com/GoogleChrome/puppeteer/issues/1132
    static async _isRedirect(browser, url) {
        let page;
        try {
            page = await browser.newPage();
            await page.setRequestInterception(true);
            let isRedirect = false;

            page.on('request', request => {
                if (request.isNavigationRequest() && request.redirectChain().length) {
                    isRedirect = true;
                    request.respond(200);
                } else request.continue();
            });

            await page.goto(url);
            return isRedirect;
        } finally {
            if (page) await page.close();
        }
    }

    static async _cleanBrowser(browser) {
        if (browser) {
            let openPages = await browser.pages();
            for (let index = 0; index < openPages.length; index++) {
                await openPages[index].close();
            }
            await browser.disconnect();
        }
    }

}

module.exports = Minifier;
