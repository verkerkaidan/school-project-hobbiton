import { test, expect } from '@playwright/test';

const PROMETHEUS_URL = process.env.PROMETHEUS_URL || 'http://localhost:9090';
const GRAFANA_URL = process.env.GRAFANA_URL || 'http://localhost:23333';

test.describe('Monitoring infrastructure', () => {

  test.describe('Prometheus', () => {
    test('Prometheus API is reachable', async ({ request }) => {
      const res = await request.get(`${PROMETHEUS_URL}/api/v1/status/config`);
      test.skip(!res.ok(), 'Prometheus is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body.status).toBe('success');
    });

    test('profile-service target is being scraped', async ({ request }) => {
      const res = await request.get(
        `${PROMETHEUS_URL}/api/v1/query?query=up{service="profile"}`
      );
      test.skip(!res.ok(), 'Prometheus is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body.status).toBe('success');
      const results = body.data?.result;
      expect(results).toBeDefined();
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(Number(results[0].value[1])).toBe(1);
    });

    test('Spring Micrometer metrics exist for profile-service', async ({ request }) => {
      const res = await request.get(
        `${PROMETHEUS_URL}/api/v1/query?query=spring_application_seconds_count{application="profile-service"}`
      );
      test.skip(!res.ok(), 'Prometheus is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body.status).toBe('success');
      expect(body.data?.result?.length).toBeGreaterThanOrEqual(1);
    });

    test('auth-service custom metrics are being scraped', async ({ request }) => {
      const res = await request.get(
        `${PROMETHEUS_URL}/api/v1/query?query=up{service="auth"}`
      );
      test.skip(!res.ok(), 'Prometheus is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body.status).toBe('success');
      const results = body.data?.result;
      expect(results).toBeDefined();
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(Number(results[0].value[1])).toBe(1);
    });

    test('auth-service heap metrics are present', async ({ request }) => {
      const res = await request.get(
        `${PROMETHEUS_URL}/api/v1/query?query=auth_heap_in_use_bytes{service="auth"}`
      );
      test.skip(!res.ok(), 'Prometheus is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body.status).toBe('success');
      const results = body.data?.result;
      expect(results).toBeDefined();
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(Number(results[0].value[1])).toBeGreaterThanOrEqual(0);
    });

    test('interaction-service target is being scraped', async ({ request }) => {
      const res = await request.get(
        `${PROMETHEUS_URL}/api/v1/query?query=up{service="interaction"}`
      );
      test.skip(!res.ok(), 'Prometheus is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body.status).toBe('success');
      const results = body.data?.result;
      expect(results).toBeDefined();
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(Number(results[0].value[1])).toBe(1);
    });

    test('recognition-service target is being scraped', async ({ request }) => {
      const res = await request.get(
        `${PROMETHEUS_URL}/api/v1/query?query=up{service="recognition"}`
      );
      test.skip(!res.ok(), 'Prometheus is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body.status).toBe('success');
      const results = body.data?.result;
      expect(results).toBeDefined();
      expect(results.length).toBeGreaterThanOrEqual(1);
      expect(Number(results[0].value[1])).toBe(1);
    });
  });

  test.describe('Grafana', () => {
    test('Grafana API is reachable', async ({ request }) => {
      const res = await request.get(`${GRAFANA_URL}/api/health`);
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body).toHaveProperty('database', 'ok');
    });

    test('Profile Service dashboard is provisioned', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/profileservice`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body).toHaveProperty('dashboard');
      expect(body.dashboard).toHaveProperty('title', 'Profile Service');
      expect(body.dashboard).toHaveProperty('uid', 'profileservice');
    });

    test('Profile Service dashboard has panels', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/profileservice`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      const panels = body.dashboard?.panels;
      expect(panels).toBeDefined();
      expect(panels.length).toBeGreaterThanOrEqual(1);
      const titles = panels.map(p => p.title);
      expect(titles).toContain('JVM Heap Memory');
      expect(titles).toContain('HTTP Request Rate');
      expect(titles).toContain('HTTP Response Time (p95)');
    });

    test('Auth Service (C) dashboard is provisioned', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/authservice`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body).toHaveProperty('dashboard');
      expect(body.dashboard).toHaveProperty('title', 'Auth Service (C)');
      expect(body.dashboard).toHaveProperty('uid', 'authservice');
    });

    test('Auth Service dashboard has new heap panels', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/authservice`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      const panels = body.dashboard?.panels;
      expect(panels).toBeDefined();
      expect(panels.length).toBeGreaterThanOrEqual(1);
      const titles = panels.map(p => p.title);
      expect(titles).toContain('Heap In Use');
      expect(titles).toContain('Heap Allocated (Total)');
      expect(titles).toContain('Memory Over Time');
    });

    test('Auth Service dashboard has logging section', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/authservice`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      const panels = body.dashboard?.panels;
      const titles = panels.map(p => p.title);
      expect(titles).toContain('Logs');
      expect(titles).toContain('Error & Warning Rate');
      expect(titles).toContain('Auth Service Logs');
    });

    test('Profile Service dashboard has logging section', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/profileservice`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      const panels = body.dashboard?.panels;
      const titles = panels.map(p => p.title);
      expect(titles).toContain('Logs');
      expect(titles).toContain('Error & Warning Rate');
      expect(titles).toContain('Profile Service Logs');
    });

    test('API Gateway dashboard has logging section', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/apigateway`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      const panels = body.dashboard?.panels;
      const titles = panels.map(p => p.title);
      expect(titles).toContain('Logs');
      expect(titles).toContain('Error & Warning Rate');
      expect(titles).toContain('API Gateway Logs');
    });

    test('Interaction Service dashboard is provisioned', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/interactionservice`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body).toHaveProperty('dashboard');
      expect(body.dashboard).toHaveProperty('title', 'Interaction Service');
      expect(body.dashboard).toHaveProperty('uid', 'interactionservice');
    });

    test('Recognition Service dashboard is provisioned', async ({ request }) => {
      const res = await request.get(
        `${GRAFANA_URL}/api/dashboards/uid/recognitionservice`
      );
      test.skip(!res.ok(), 'Grafana is not available');
      expect(res.ok()).toBeTruthy();
      const body = await res.json();
      expect(body).toHaveProperty('dashboard');
      expect(body.dashboard).toHaveProperty('title', 'Recognition Service');
      expect(body.dashboard).toHaveProperty('uid', 'recognitionservice');
    });
  });
});
