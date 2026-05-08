# Legalstaan-LLC GitHub Pages deploy
# Restructures the repo so URLs are: legalstaan.com/privacy, /terms, /account-deletion, /support
# Run from this folder:  .\deploy_to_legalstaan_llc.ps1

$ErrorActionPreference = "Stop"

$repoUrl   = "https://github.com/DHANANAN/Legalstaan-LLC.git"
$workDir   = "$env:TEMP\legalstaan-llc-deploy"
$srcDir    = $PSScriptRoot   # this folder = play-store-listing/

Write-Host "[1/7] Cleaning previous workspace..." -ForegroundColor Cyan
if (Test-Path $workDir) { Remove-Item $workDir -Recurse -Force }

Write-Host "[2/7] Cloning $repoUrl ..." -ForegroundColor Cyan
git clone $repoUrl $workDir
Set-Location $workDir

Write-Host "[3/7] Removing old flat-named files at repo root..." -ForegroundColor Cyan
foreach ($f in @("Piracypolicy.html", "privacypolicy.html", "accountdeletion.html", "terms.html", "support.html")) {
    if (Test-Path $f) { git rm $f }
}

Write-Host "[4/7] Creating clean folder structure..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path "privacy", "terms", "account-deletion", "support" | Out-Null

Copy-Item "$srcDir\privacy_policy.html"   "privacy\index.html"           -Force
Copy-Item "$srcDir\terms.html"            "terms\index.html"             -Force
Copy-Item "$srcDir\account_deletion.html" "account-deletion\index.html"  -Force
Copy-Item "$srcDir\support.html"          "support\index.html"           -Force
Copy-Item "$srcDir\index.html"            "index.html"                   -Force

Write-Host "[5/7] Removing CNAME (we are hosting on dhananan.github.io subdomain, no custom domain)..." -ForegroundColor Cyan
if (Test-Path "CNAME") { git rm CNAME }

Write-Host "[6/7] Committing changes..." -ForegroundColor Cyan
git add -A
git status --short
$commitMsg = "Restructure for GitHub Pages: folder-per-page with relative nav links"
git commit -m $commitMsg

Write-Host "[7/7] Pushing to origin/main..." -ForegroundColor Cyan
Write-Host "      (you'll be prompted to authenticate to GitHub)" -ForegroundColor Yellow
git push origin main

Write-Host "`nDONE." -ForegroundColor Green
Write-Host "Next steps:"
Write-Host "  1. GitHub.com -> DHANANAN/Legalstaan-LLC -> Settings -> Pages"
Write-Host "     Source: Deploy from a branch. Branch: main / (root). Save."
Write-Host "  2. Wait ~30s. Site will be live at:"
Write-Host "       https://dhananan.github.io/Legalstaan-LLC/"
Write-Host "       https://dhananan.github.io/Legalstaan-LLC/privacy/"
Write-Host "       https://dhananan.github.io/Legalstaan-LLC/terms/"
Write-Host "       https://dhananan.github.io/Legalstaan-LLC/account-deletion/"
Write-Host "       https://dhananan.github.io/Legalstaan-LLC/support/"
Write-Host "  3. Paste the privacy + account-deletion URLs into Play Console."
