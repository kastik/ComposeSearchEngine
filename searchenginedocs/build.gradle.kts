plugins {
    id ("org.danilopianini.gradle-latex") version "0.2.7" // Exemplificatory, pick the last stable one!
}



latex {
    terminalEmulator.set("zsh") // Your terminal
    waitTime.set(1) // How long before considering a process stalled
    waitUnit.set(TimeUnit.MINUTES) // Time unit for the number above
    pdfLatexCommand.set("pdflatex")
    bibTexCommand.set("bibtex")
    "src/report" {
        // Options for pdflatex
        extraArguments = listOf("-shell-escape", "-synctex=1", "-interaction=nonstopmode") //"-halt-on-error")

        /*
         * Additional files and directories whose change should trigger a build in case gradle is used with -t flag.
         * Can be passed files, strings, or any Object compatible with Gradle's project.files
         */
    }
    terminalEmulator.set("zsh") // Your terminal
    waitTime.set(1) // How long before considering a process stalled
    waitUnit.set(TimeUnit.MINUTES) // Time unit for the number above
    pdfLatexCommand.set("lulatex")
    bibTexCommand.set("bibtex")
    "src/report_greek" {
        // Options for pdflatex
        extraArguments = listOf("-shell-escape", "-synctex=1", "-interaction=nonstopmode") //"-halt-on-error")

        /*
         * Additional files and directories whose change should trigger a build in case gradle is used with -t flag.
         * Can be passed files, strings, or any Object compatible with Gradle's project.files
         */
    }
}