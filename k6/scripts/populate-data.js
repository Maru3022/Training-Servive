import http from 'k6/http';
import { check, sleep } from 'k6';

export default function () {
    // URL правильный, мы уже проверили
    const url = 'http://localhost:8080/trainings';

    // МЕНЯЕМ ПОЛЯ НА SNAKE_CASE (как в базе данных)
    const payload = JSON.stringify({
        training_name: 'K6 Snake Case Test', 
        status: 'PLANNED',
        data: '2026-01-12', 
        user_id: '550e8400-e29b-41d4-a716-446655440000'
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const res = http.post(url, payload, params);

    if (res.status !== 201 && res.status !== 200) {
        console.log('Error! Status: ' + res.status);
        console.log('Response: ' + res.body);
    }

    check(res, {
        'is status 201': (r) => r.status === 201,
    });

    sleep(0.5);
}
