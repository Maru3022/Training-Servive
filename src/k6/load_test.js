import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    scenarios: {
        bulk_load: {
            executor: 'per-vu-iterations',
            vus: 100,              // 100 параллельных потоков
            iterations: 10000,     // каждый сделает по 10 000 запросов
            maxDuration: '2h',     // лимит времени — 2 часа
        },
    },
};

export default function () {
    // Убрали /api, так как в коде контроллера его нет
    const url = 'http://localhost:8080/trainings';

    const payload = JSON.stringify({
        id: uuidv4(),
        userId: uuidv4(),
        data: "2026-01-12",
        status: "PLANNED",
        training_name: "Power Training #" + Math.floor(Math.random() * 1000000)
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'is status 201': (r) => r.status === 201,
    });
}