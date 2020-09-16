# --------------------------------------------------------------------------
#   ██╗  ██╗███████╗
#   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
#   █████╔╝ █████╗     KFoundation for Scala Library
#   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
#   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
#   ╚═╝  ╚═╝╚═╝
# --------------------------------------------------------------------------

#!/usr/bin/env sh
export GPG_TTY=$(tty)
sbt --no-colors --supershell=false "+publishLocal" "+publishM2" "+publishSigned" "fullOptJS"