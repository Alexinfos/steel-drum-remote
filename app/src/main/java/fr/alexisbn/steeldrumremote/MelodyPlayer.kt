package fr.alexisbn.steeldrumremote

import kotlinx.coroutines.delay

class MelodyPlayer(private val bluetoothService: BluetoothService) : Thread() {

    private var continueRunning = true
    private var isRunning = false;

    private val instructions : List<MusicInstruction> = arrayOf(
        MusicInstruction(listOf(5), 0.5),     // G5
        MusicInstruction(listOf(4), 0.5),     // F5
        MusicInstruction(listOf(3), 1.0),     // C5

        MusicInstruction(listOf(2), 0.5),     // Bb4
        MusicInstruction(listOf(3), 0.5),     // C5
        MusicInstruction(listOf(5), 1.0),     // G5

        MusicInstruction(listOf(7), 0.5),     // Bb5
        MusicInstruction(listOf(6), 0.5),     // C6
        MusicInstruction(listOf(5), 1.0),     // G5

        MusicInstruction(listOf(1), 0.5),     // D5
        MusicInstruction(listOf(3), 0.5),     // C5
        MusicInstruction(listOf(4), 1.0),     // F5

// Second phrase with a bit more rhythmic complexity
        MusicInstruction(listOf(2), 0.5),     // Bb4
        MusicInstruction(listOf(8), 0.5),     // G4
        MusicInstruction(listOf(1), 1.0),     // D5

        MusicInstruction(listOf(3), 0.5),     // C5
        MusicInstruction(listOf(5), 0.5),     // G5
        MusicInstruction(listOf(7), 1.0),     // Bb5

        MusicInstruction(listOf(6), 0.25),    // C6
        MusicInstruction(listOf(5), 0.25),    // G5
        MusicInstruction(listOf(4), 0.5),     // F5
        MusicInstruction(listOf(3), 1.0),     // C5

// Ending cadence
        MusicInstruction(listOf(2), 0.5),     // Bb4
        MusicInstruction(listOf(8), 0.5),     // G4
        MusicInstruction(listOf(1), 1.0),     // D5
    ).toList()

    override fun run() {
        isRunning = true;
        var currentIndex = 0;
        while (continueRunning) {
            val instr = instructions[currentIndex % instructions.size]
            var notesString = ""
            for (n in instr.notes) {
                notesString += " $n"
            }
            bluetoothService.sendCommand("play_multiple ${instr.notes.size}$notesString")
            sleep((instr.duration * 1000).toLong())
            currentIndex++
        }
        isRunning = false;
    }

    fun stopMelody() {
        continueRunning = false
    }

    fun isRunning(): Boolean {
        return isRunning
    }
}