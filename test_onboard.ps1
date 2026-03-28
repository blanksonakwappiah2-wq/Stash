try {
    Write-Host "Logging in as manager..."
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/users/login" -Method Post -Body '{"email":"manager@quickbite.com","password":"Manager123"}' -ContentType "application/json"
    $token = $response.token
    Write-Host "Successfully logged in. Token length: $($token.Length)"

    Write-Host "Onboarding agent..."
    $agentResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/delivery/agents" -Method Post -Body '{"name":"Test Agent","email":"testagent@gmail.com","password":"password123"}' -ContentType "application/json" -Headers @{"Authorization"="Bearer $($token)"}
    
    Write-Host "Onboarding Success. Response:"
    $agentResponse | ConvertTo-Json
} catch {
    Write-Host "Error occurred:"
    Write-Host $_.Exception.Message
    if ($_.ErrorDetails) {
        Write-Host "Error Details: $($_.ErrorDetails.Message)"
    }
}
