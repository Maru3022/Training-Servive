import axios from 'axios';

export const apiClient = axios.create({
    baseURL: 'http://localhost:8085', // Порт вашего бэкенда
    headers: {
        'Content-Type': 'application/json',
    },
});