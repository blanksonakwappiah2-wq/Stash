Write-Host "Killing 8080..."
try { $p = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess; if ($p) { Stop-Process -Id $p -Force } } catch {}

Write-Host "Starting server and piping to spring.log..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/c ..\apache-maven-3.9.14\bin\mvn.cmd spring-boot:run > spring.log 2>&1" -WindowStyle Hidden -WorkingDirectory "c:\Users\user\Desktop\New folder\backend"

Write-Host "Waiting 15s for startup..."
Start-Sleep -Seconds 15

Write-Host "Testing endpoint..."
try {
    $r = Invoke-RestMethod -Uri "http://localhost:8080/api/users/login" -Method Post -Body '{"email":"manager@quickbite.com","password":"Manager123"}' -ContentType "application/json"
    $t = $r.token
    Invoke-RestMethod -Uri "http://localhost:8080/api/delivery/agents" -Method Post -Body '{"name":"Test Agent","email":"testagent@gmail.com","password":"password123"}' -ContentType "application/json" -Headers @{"Authorization"="Bearer $t"}
} catch {
    Write-Host "Caught expected 500 error during onboard."
}
Write-Host "Finished executing bug reproduction flow."
