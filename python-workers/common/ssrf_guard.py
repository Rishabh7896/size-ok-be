"""
Idempotent SQS Product Extraction Worker with SSRF protection.
Processes product URL ingestion events and extracts JSON-LD metadata and size charts.
"""
import ipaddress
import socket
from urllib.parse import urlparse


def validate_public_url(url: str) -> str:
    parsed = urlparse(url)
    if parsed.scheme not in ("http", "https"):
        raise ValueError(f"Unsupported scheme: {parsed.scheme}")
    host = (parsed.hostname or "").lower()
    if not host or host in ("localhost", "169.254.169.254", "metadata.google.internal") or host.endswith(".internal"):
        raise ValueError(f"Blocked SSRF host: {host}")
    try:
        ip = ipaddress.ip_address(host)
        if ip.is_private or ip.is_loopback or ip.is_link_local or ip.is_multicast or ip.is_reserved:
            raise ValueError(f"Blocked non-public IP: {ip}")
    except ValueError as exc:
        if "Blocked" in str(exc):
            raise
    return url
