import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    stages: [
        { duration: '1m', target: 300 },  // Разгон
        { duration: '1m', target: 1000 }, // Нагрузка
        { duration: '2m', target: 2000 }, // Пик
        { duration: '1m', target: 0 },    // Спад
    ],
};

export default function () {
    const url = 'http://localhost:8085/trainings';

    const payload = JSON.stringify({
        userId: uuidv4(),
        training_name: `Stress #${Math.floor(Math.random() * 1000000)}`, // Теперь точно как в валидаторе
        data: "2026-01-20",
        status: "PLANNED",
        exercises: []
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        timeout: '60s',
    };

    const res = http.post(url, payload, params);

    // Логируем только реальные ошибки (не 201/202)
    if (res.status !== 202 && res.status !== 201) {
        console.error(`Status: ${res.status}, Body: ${res.body}`);
    }

    check(res, {
        'is accepted': (r) => r.status === 202 || r.status === 201,
    });

    sleep(0.1);
}