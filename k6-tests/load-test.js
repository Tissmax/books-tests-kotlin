import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 20 }, // Montée rapide pour ton test
        { duration: '30s', target: 20 },
        { duration: '10s', target: 0 },
    ],
};

export default function () {
    const url = 'http://localhost:8080/books';

    // On utilise "titre" et "auteur" comme défini dans ton BookDTO.kt
    const payload = JSON.stringify({
        titre: `Livre de Performance ${Math.floor(Math.random() * 1000)}`,
        auteur: 'K6 Tester'
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status is 201': (r) => r.status === 201,
        'transaction time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}