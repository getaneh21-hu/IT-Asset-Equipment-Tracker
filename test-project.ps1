
$ErrorActionPreference = "Stop"

if (-not (Test-Path ".\.env")) {
    throw ".env file not found. Run this script from the project folder."
}

Get-Content ".\.env" | ForEach-Object {
    if ($_ -match '^\s*(DB_PASSWORD|JWT_SECRET)\s*=\s*(.*)\s*$') {
        $name = $matches[1]
        $value = $matches[2].Trim()

        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
}

if (-not $env:DB_PASSWORD -or -not $env:JWT_SECRET) {
    throw "DB_PASSWORD or JWT_SECRET is missing from .env."
}

& .\mvnw.cmd test
exit $LASTEXITCODE
