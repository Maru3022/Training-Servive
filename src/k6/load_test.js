import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    stages: [
        { duration: '30s', target: 500 },  // Разгон
        { duration: '1m', target: 1500 }, // Нагрузка
        { duration: '30s', target: 0 },    // Спад
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],   // Ошибок меньше 1%
        http_req_duration: ['p(95)<500'], // 95% запросов быстрее 500мс
    },
};

export default function () {
    const url = 'http://localhost:8085/trainings';

    const payload = JSON.stringify({
        userId: uuidv4(),
        training_name: `DB-Test-${Math.floor(Math.random() * 1000000)}`,
        data: "2026-01-22",
        status: "PLANNED",
        exercises: []
    });

    const params = { headers: { 'Content-Type': 'application/json' } };

    // 1. Тестируем ЗАПИСЬ в Postgres
    const res = http.post(url, payload, params);

    check(res, {
        'post accepted': (r) => r.status === 201 || r.status === 202,
    });

    // 2. Тестируем ЧТЕНИЕ из Postgres (без Redis это будет дольше)
    if (res.status === 201 && res.json().id) {
        const id = res.json().id;
        const getRes = http.get(`${url}/${id}`);
        check(getRes, { 'get successful': (r) => r.status === 200 });
    }

    sleep(1);
}