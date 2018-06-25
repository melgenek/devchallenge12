'use strict';

const amqp = require('amqplib');
const Minifier = require('./minifier');

const minifyQueueName = 'minify';

const AMQP_HOST = process.env.AMQP_HOST || 'localhost';
const AMQP_PORT = process.env.AMQP_PORT || 5672;
const AMQP_USER = process.env.AMQP_USER || 'rabbit';
const AMQP_PASSWORD = process.env.AMQP_PASSWORD || 'rabbit123';

startListener();

async function startListener() {
    try {
        const connection = await amqp.connect(`amqp://${AMQP_USER}:${AMQP_PASSWORD}@${AMQP_HOST}:${AMQP_PORT}`);
        connection.on('error', retry);

        const channel = await connection.createChannel();
        await channel.assertQueue(minifyQueueName, {durable: false});
        await channel.prefetch(1);

        const minCallback = callback(channel);

        await channel.consume(minifyQueueName, minCallback, {noAck: false});
        console.log("Listener started");
    } catch (e) {
        console.error(e.name + ' ' + e.message);
        retry();
    }
}

function retry() {
    setTimeout(startListener, 2000);
}

const callback = (ch) => (msg) => {
    const content = msg.content.toString();
    const message = JSON.parse(content);
    console.log(`Received message ${content}`);
    Minifier.minifyCss(message.url)
        .then(result => {
            console.log(`Success '${message.url}'. Is redirect: ${result.redirect}`);
            return {
                success: true,
                url: message.url,
                css: result.result,
                redirect: result.redirect
            };
        })
        .catch((e) => {
            console.error(`Error '${message.url}': `, e);
            return {
                success: false,
                url: message.url,
                error: e.message
            };
        })
        .then(response => {
            ch.sendToQueue(msg.properties.replyTo,
                Buffer.from(JSON.stringify(response)),
                {correlationId: msg.properties.correlationId})
        }).then(() => ch.ack(msg));
};

