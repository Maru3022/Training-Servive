import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
    // Настройка этапов нагрузки
    stages: [
        { duration: '1m', target: 500 },   // Плавный разогрев до 500 за 1 минуту
        { duration: '2m', target: 1500 },  // Подъем до пика в 1500 за 2 минуты
        { duration: '1m', target: 1500 },  // Удержание пиковой нагрузки 1 минуту (проверка стабильности)
        { duration: '1m', target: 0 },     // Плавное снижение
    ],

    // Глобальный таймаут тестов
    setupTimeout: '120s',

    // Пороги успешности (Fail, если ошибок > 1% или 95% запросов медленнее 500мс)
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
    // Генерация случайных данных
    const payload = JSON.stringify({
        userId: uuidv4(),
        training_name: `Stress Test 1500 VU #${Math.floor(Math.random() * 1000000)}`,
        data: "2026-01-20",
        status: "PLANNED",
        exercises: []
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
        // Таймаут самого запроса
        timeout: '60s',
    };

    const res = http.post('http://localhost:8085/trainings', payload, params);

    // Проверка, что сервер принял запрос (201 Created или 202 Accepted)
    check(res, {
        'status is 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    // Небольшая пауза, чтобы эмулировать реальное поведение пользователя
    // и не забить TCP-порты локальной машины
    sleep(1);
}