import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomUUID } from 'k6/crypto';

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------
const BASE_URL = __ENV.BASE_URL || 'http://training-service.training.svc.cluster.local:8085';

export const options = {
  // Ramp-up → steady → ramp-down
  stages: [
    { duration: '30s', target: 10  },  // warm-up
    { duration: '1m',  target: 30  },  // ramp up
    { duration: '2m',  target: 50  },  // steady load
    { duration: '30s', target: 80  },  // spike
    { duration: '1m',  target: 50  },  // back to steady
    { duration: '30s', target: 0   },  // ramp-down
  ],
  thresholds: {
    // 95% of requests must complete below 1.5 s
    'http_req_duration': ['p(95)<1500'],
    // Error rate must stay below 5%
    'http_req_failed': ['rate<0.05'],
    // All checks must pass at ≥ 99%
    'checks': ['rate>0.99'],
  },
};

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------
const headers = { 'Content-Type': 'application/json' };

function randomStatus() {
  const statuses = ['PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
  return statuses[Math.floor(Math.random() * statuses.length)];
}

function randomMuscle() {
  const groups = ['CHEST', 'BACK', 'LEGS', 'SHOULDERS', 'ARMS', 'CORE', 'FULL_BODY'];
  return groups[Math.floor(Math.random() * groups.length)];
}

function trainingPayload(userId) {
  return JSON.stringify({
    training_date: '2025-06-01',
    user_id: userId,
    training_name: `Load Test Training ${Math.floor(Math.random() * 10000)}`,
    training_status: 'PLANNED',
    sets: [
      { exercise_id: randomUUID(), weight: 80, reps: 10, order_: 1 },
      { exercise_id: randomUUID(), weight: 60, reps: 12, order_: 2 },
    ],
  });
}

function userPayload() {
  const uid = Math.floor(Math.random() * 1000000);
  return JSON.stringify({
    username:  `user_${uid}`,
    email:     `user_${uid}@test.com`,
    fullName:  `Test User ${uid}`,
    role:      'ATHLETE',
  });
}

function exercisePayload() {
  return JSON.stringify({
    name:        `Exercise ${Math.floor(Math.random() * 100000)}`,
    description: 'Load test exercise',
    muscleGroup: randomMuscle(),
  });
}

// ---------------------------------------------------------------------------
// Scenario: create user → create training → get training → list trainings
// ---------------------------------------------------------------------------
export default function () {
  // 1. Create user
  const createUserRes = http.post(`${BASE_URL}/users`, userPayload(), { headers });
  const userOk = check(createUserRes, {
    'POST /users → 201': (r) => r.status === 201,
  });

  let userId = null;
  if (userOk) {
    userId = createUserRes.json('id');
  }

  sleep(0.2);

  // 2. Create exercise
  const createExRes = http.post(`${BASE_URL}/exercises`, exercisePayload(), { headers });
  check(createExRes, {
    'POST /exercises → 201': (r) => r.status === 201,
  });

  sleep(0.2);

  // 3. Create training (needs a userId)
  if (userId) {
    const createTrRes = http.post(
      `${BASE_URL}/trainings`,
      trainingPayload(userId),
      { headers },
    );
    const trainingCreated = check(createTrRes, {
      'POST /trainings → 202': (r) => r.status === 202,
    });

    sleep(0.3);

    // 4. GET /trainings?userId=...&status=PLANNED
    const listRes = http.get(
      `${BASE_URL}/trainings?userId=${userId}&status=PLANNED&page=0&size=10`,
    );
    check(listRes, {
      'GET /trainings list → 200': (r) => r.status === 200,
    });

    sleep(0.2);

    // 5. GET /users/{id}/stats
    const statsRes = http.get(`${BASE_URL}/users/${userId}/stats`);
    check(statsRes, {
      'GET /users/:id/stats → 200': (r) => r.status === 200,
    });
  }

  // 6. GET /exercises (paginated)
  const exListRes = http.get(`${BASE_URL}/exercises?page=0&size=20`);
  check(exListRes, {
    'GET /exercises → 200': (r) => r.status === 200,
  });

  sleep(0.5);
}
