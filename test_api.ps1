$body = @{email="admin@asset.com"; password="admin123"}
$json = $body | ConvertTo-Json
$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -Body $json -ContentType "application/json" -SessionVariable session
$res2 = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/notifications" -WebSession $session
$res2 | ConvertTo-Json -Depth 5
