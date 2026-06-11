param(
    [Parameter(Mandatory = $true)]
    [string]$GitName,

    [Parameter(Mandatory = $true)]
    [string]$GitEmail,

    [string]$CommitMessage = "chore: initialize secondary development scaffold",

    [switch]$Force
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$gitDir = Join-Path $repoRoot ".git"

if ([string]::IsNullOrWhiteSpace($GitName) -or [string]::IsNullOrWhiteSpace($GitEmail)) {
    throw "GitName and GitEmail must not be empty."
}

if (-not $Force) {
    $answer = Read-Host "Reset Git history in '$repoRoot'? Type RESET to continue"
    if ($answer -ne "RESET") {
        throw "Cancelled."
    }
}

python (Join-Path $repoRoot "tools/repository_audit.py")
if ($LASTEXITCODE -ne 0) {
    throw "Repository audit failed. Review findings before initializing Git."
}

if (Test-Path -LiteralPath $gitDir) {
    $resolvedGitDir = (Resolve-Path -LiteralPath $gitDir).Path
    if (-not $resolvedGitDir.StartsWith($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to remove Git directory outside repository root."
    }
    Remove-Item -LiteralPath $resolvedGitDir -Recurse -Force
}

git -C $repoRoot init -b main
git -C $repoRoot config user.name $GitName
git -C $repoRoot config user.email $GitEmail
git -C $repoRoot add .
git -C $repoRoot diff --cached --check
git -C $repoRoot commit -m $CommitMessage

Write-Host "Initialized repository at $repoRoot"
