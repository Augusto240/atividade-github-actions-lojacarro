import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '20s', target: 10 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<300'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    const res = http.get(`${BASE_URL}/boas-vindas`);

    check(res, {
        'status é 200': (r) => r.status === 200,
        'corpo tem mensagem de boas vindas': (r) => r.json('mensagem') === 'Bem-vindo a LojaCarro API!',
    });

    sleep(1);
}
