import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 }, // разгон до 20 пользователей
    { duration: '1m', target: 20 },  // стабильная нагрузка 20 пользователей
    { duration: '30s', target: 50 }, // рывок до 50 пользователей
    { duration: '1m', target: 50 },  // проверка на выносливость
    { duration: '30s', target: 0 },  // остановка
  ],
};

export default function () {
  // Используем тестовый URL k6, чтобы не положить ваш сервис раньше времени
  const res = http.get('https://test.k6.io');
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
