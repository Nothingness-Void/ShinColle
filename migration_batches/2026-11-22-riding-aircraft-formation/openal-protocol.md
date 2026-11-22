# Explicit OpenAL stop audit

After line-abreast, search modern Java (including client event/renderer/network/
sound helpers) for SoundManager.stop, stopAll, stopsound, SoundInstance and custom
loop instances/tickable sounds; inspect dispatch sites found. Compare corresponding
legacy stop behavior only if a mod stop path exists. Inspect vanilla Channel.stop/
destroy checkALError Stop when needed. No speculative audio workaround; absence
of a mod stop site means vanilla/unattributed, not a confirmed vanilla bug.
Full user's client log/call stack is not available; state reachability limitation.
