const http = require('http');

const data = JSON.stringify({ email: 'admin@asset.com', password: 'admin123' });

const req = http.request({
  hostname: 'localhost',
  port: 8080,
  path: '/api/auth/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': data.length
  }
}, (res) => {
  let cookie = res.headers['set-cookie'] ? res.headers['set-cookie'][0] : '';
  
  const req2 = http.request({
    hostname: 'localhost',
    port: 8080,
    path: '/api/notifications',
    method: 'GET',
    headers: {
      'Cookie': cookie
    }
  }, (res2) => {
    let body = '';
    res2.on('data', chunk => body += chunk);
    res2.on('end', () => console.log("OUTPUT:", body));
  });
  req2.end();
});

req.write(data);
req.end();
