"""Compare all native Java fixtures byte-for-byte with the unmodified upstream generator."""
import sys
from pathlib import Path
from datetime import datetime, timezone
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "upstream"))
import fsc_generator as fsc

fixtures = Path(sys.argv[1])
stamp = datetime(2026, 11, 13, 12, 0, tzinfo=timezone.utc)
for feature in fsc.FEATURES:
    data, doc = fsc.generate("AB12345", feature.application_id, feature.upgrade_index, timestamp=stamp)
    native = (fixtures / doc.filename).read_bytes()
    assert native == data, feature.label
    assert fsc.verify_document(native, fsc.load_signing_key().public_key()).signature == "verified"
print(f"PASS: all {len(fsc.FEATURES)} native outputs match upstream Python exactly, including RSA signatures.")
