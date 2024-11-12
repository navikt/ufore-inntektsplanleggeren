import {createClient} from "redis";

let isReady = false;

export const redisClient = createClient({
    host: process.env.REDIS_URI_PLANLEGGER_PERSISTANCE_E62B9661,
    port: process.env.REDIS_PORT_PLANLEGGER_PERSISTANCE_E62B9661,
    user: process.env.REDIS_USERNAME_PLANLEGGER_PERSISTANCE_E62B9661,
    password: process.env.REDIS_PASSWORD_PLANLEGGER_PERSISTANCE_E62B9661,
    pingInterval: 3_000,
}).on('error', err => console.log('Redis Client Error', err));

export const initRedis = async () => {
    await redisClient.connect();
    isReady = true;
};

export const isRedisReady = () => isReady;
