# --------------------------------------------------------------------------
#   ██╗  ██╗███████╗
#   ██║ ██╔╝██╔════╝   The KFoundation Project (www.kfoundation.net)
#   █████╔╝ █████╗     KFoundation for Scala Library
#   ██╔═██╗ ██╔══╝     Copyright (c) 2020 Mindscape Inc.
#   ██║  ██╗██║        Terms of KnoRBA Free Public License Agreement Apply
#   ╚═╝  ╚═╝╚═╝
# --------------------------------------------------------------------------

#!/usr/bin/env sh
sbt --supershell=false compile package "+publishLocal" "+publishM2"