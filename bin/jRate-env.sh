#
# jRate Environment Settings
#

#
# Set the following environment variable to the directory under which
# you want jRate software suite to be downloaded and installed.
# A sample setting could be:
#
#      export JRATE_SUITE_HOME=/home/angelo/Devel
#

export JRATE_SUITE_HOME=/home/jay/


#######################################################################
#
# NOTE: You don't need to change the following environment variable
# unless you want to change the default location for the various jRate
# components.
#
#######################################################################

#
# jRate Settings
#
export JRATE_HOME=$JRATE_SUITE_HOME/jRate
export JRATE_GCC_HOME=$JRATE_SUITE_HOME/jRate-gcc
export JRATE_GCC_SRC_HOME=$JRATE_SUITE_HOME/GNU/jRateGCC

export LD_LIBRARY_PATH=$JRATE_HOME/lib:$LD_LIBRARY_PATH

export PATH=$JRATE_GCC_HOME/bin:$PATH
export PATH=$JRATE_GCC_HOME/bin:$PATH
export LD_LIBRARY_PATH=$JRATE_GCC_HOME/lib:$LD_LIBRARY_PATH

export MANPATH=$JRATE_GCC_HOME/man:$MANPATH
