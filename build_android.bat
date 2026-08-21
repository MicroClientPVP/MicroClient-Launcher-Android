@echo off
echo Building MicroClient Launcher (Android)...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo Gradle build failed!
    pause
    exit /b %errorlevel%
)

echo Android App built successfully at app_pojavlauncher\build\outputs\apk\debug\app_pojavlauncher-debug.apk !

set /p PUBLISH="Do you want to publish the Android APK to the CDN? (y/n): "
if /i not "%PUBLISH%"=="y" goto skip_publish
echo Publishing to CDN (chunked to bypass Cloudflare 100MB limit)...

echo $file = 'app_pojavlauncher\build\outputs\apk\debug\app_pojavlauncher-debug.apk' > temp_upload.ps1
echo $chunkSize = 50MB >> temp_upload.ps1
echo $bytes = [System.IO.File]::ReadAllBytes($file) >> temp_upload.ps1
echo $totalChunks = [Math]::Ceiling($bytes.Length / $chunkSize) >> temp_upload.ps1
echo for ($i=0; $i -lt $totalChunks; $i++) { >> temp_upload.ps1
echo     $start = $i * $chunkSize >> temp_upload.ps1
echo     $end = [Math]::Min($start + $chunkSize, $bytes.Length) >> temp_upload.ps1
echo     $chunkBytes = $bytes[$start..($end-1)] >> temp_upload.ps1
echo     $tempFile = 'temp_chunk.bin' >> temp_upload.ps1
echo     [System.IO.File]::WriteAllBytes($tempFile, $chunkBytes) >> temp_upload.ps1
echo     $append = if ($i -eq 0) { 'false' } else { 'true' } >> temp_upload.ps1
echo     Write-Host "Uploading chunk $(($i+1))/$totalChunks..." >> temp_upload.ps1
echo     curl.exe -s -F "api_key=%MICROCLIENT_UPLOAD_KEY%" -F "append=$append" -F "file=@$tempFile" https://cdn-microclient.komas19.party/upload_android.php >> temp_upload.ps1
echo     Remove-Item $tempFile >> temp_upload.ps1
echo } >> temp_upload.ps1
powershell -ExecutionPolicy Bypass -File temp_upload.ps1
del temp_upload.ps1

echo.
:skip_publish

pause
