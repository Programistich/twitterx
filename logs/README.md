# Logs Directory

This directory contains the log files for the TwitterX application.

## Log Files

- `twitterx.log` - Main application log file
- `twitterx.YYYY-MM-DD.log` - Daily archived log files
- `error.log` - Error-only log file
- `error.YYYY-MM-DD.log` - Daily archived error log files

## Log Levels

- **DEBUG**: Detailed information for development
- **INFO**: General information about application operations
- **WARN**: Warning messages that don't stop execution
- **ERROR**: Error messages that indicate problems

## Retention Policy

- Main logs: 30 days, max 1GB total
- Error logs: 30 days, max 500MB total
- Logs are automatically rotated daily
- Old logs are automatically cleaned up after retention period

## Module-Specific Logging

- `twitterx.twitter.*` - Twitter API operations
- `twitterx.translation.*` - Translation service operations
- `twitterx.video.*` - Video processing operations