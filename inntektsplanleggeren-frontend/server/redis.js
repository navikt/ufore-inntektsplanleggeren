import {createClient} from "redis";

let isReady = false;

export const redisClient = createClient({
    url: process.env.REDIS_URI_PLANLEGGER_PERSISTENCE_E62B9661,
    username: process.env.REDIS_USERNAME_PLANLEGGER_PERSISTENCE_E62B9661,
    password: process.env.REDIS_PASSWORD_PLANLEGGER_PERSISTENCE_E62B9661,
    pingInterval: 3_000,
}).on('error', err => console.log('Redis Client Error', err));

export const initRedis = async () => {
    await redisClient.connect();
    isReady = true;
};

export const isRedisReady = () => isReady;
