# Run this from your cs446-project git repo root. cwd is important

import sys, os, fileinput

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print "Expecting source file folder as argument"
        sys.exit()

    sourceFolder = os.getcwd() + "/" + sys.argv[1]
    sourceFiles = [f for f in os.listdir(sourceFolder) if os.path.isfile(os.path.join(sourceFolder, f)) and f[-5:] == ".java"]

    for source in sourceFiles:
        sourcePath = sourceFolder + source
        for line in fileinput.input(sourcePath, inplace=True):
            sys.stdout.write(line.replace("\t", "    "));
