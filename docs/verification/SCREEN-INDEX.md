# DriveMind native screen index

Verified on an Android 15 (API 35) emulator at 1080 × 2340. The images are Android captures. Notes were seeded as synthetic rows in the debug installation. Driving indicators used injected emulator GPS fixes.

| ID | Screen or state | How to reach it | Evidence |
| --- | --- | --- | --- |
| 00 | Launch splash | Cold start the app. | [Launch splash](00-launch.png) |
| 01 | Drive | Launch DriveMind and resolve the location prompt. | [Drive](01-drive.png) |
| 02 | Location prompt | Launch with no location decision. | [Location permission](02-location-permission.png) |
| 03 | Microphone prompt | Tap the microphone before granting access. | [Microphone permission](03-microphone-permission.png) |
| 04 | Permission denied and recognition error | Deny microphone access. Tap again and grant access to retry. | [Permission denied](04-microphone-denied.png) · [Provider network error](04b-recognizer-state.png) |
| 05 | Listening, transcript and save | Tap the microphone after granting access. | [Listening](05-listening.png). The provider returned a network error before producing a transcript, so transcript preview and saved confirmation remain unverified. |
| 06 | Empty Notes | Tap Notes before creating or seeding any notes. | [Empty Notes](06-notes-empty.png) |
| 07 | Populated Notes | Open Notes with synthetic notes present. | [Notes list](07-notes-list.png) |
| 08 | Combined search and type filter | Search for `supplier`, then switch between Idea and Task. | [No matches](08-search-no-matches.png) · [Task result](08b-search-task.png) |
| 09 | Note details | Tap the task result. | [Note details](09-note-detail.png). Read aloud was invoked. Audio output remains unverified. |
| 10 | Edit and save | Tap Edit, change the text, and save. | [Edit](10-edit-note.png) · [Saved changes](10b-edited-note.png) |
| 11 | Delete confirmation | Use More options → Delete. Test Cancel, then confirm on a synthetic note. | [Delete confirmation](11-delete-confirmation.png). Confirmed removal in Notes. |
| 12 | Delete and Undo | Use the delete icon on a Notes row, then tap Undo in the message. | [Undo affordance](12-delete-undo.png) · [Restored note](12b-undo-restored.png). Confirmed the fixture was restored in Room. |
| 13 | Driving and parked indicators | Grant precise location and inject fast and slow GPS fixes in the emulator. | [Simulated driving](13-driving-simulated.png) · [Simulated parked](13b-parked-simulated.png) |

## Verification results

- JDK 17, Gradle 8.5, SDK Platform 34. The debug app and instrumentation APKs built successfully. The verification used an installed Gradle 8.5 distribution and left pre-existing wrapper edits in the local checkout.
- All 24 JVM tests and 8 Room instrumentation tests passed. The latter ran on the API 35 emulator.
- The debug APK installed and launched successfully. Its Android Debug signature passed verification. SHA-256: `bd819d3ec907fe62ae64fa4a9dcbd9125b13e650a7a976a5148bb6f4552c45ea`.
- The [public browser demo](https://drivemind-demo.vercel.app) passed capture, motion, search plus filtering, editing, simulated playback, and deletion checks in Edge. The phone frame was checked at 1200 × 860 and the full-screen view at 390 × 844.

For device follow-up, use a stationary device with working English speech recognition and text-to-speech. Verify transcript preview, a successful voice save, and audible read-aloud.
