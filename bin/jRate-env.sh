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

setenv JRATE_SUITE_HOME /home/jay/RTZen


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
setenv JRATE_HOME $JRATE_SUITE_HOME/jRate
setenv JRATE_GCC_HOME $JRATE_SUITE_HOME/jRate-gcc
setenv JRATE_GCC_SRC_HOME $JRATE_SUITE_HOME/GNU/jRateGCC

setenv LD_LIBRARY_PATH `pwd`/../lib:$JRATE_HOME/lib:$LD_LIBRARY_PATH

#setenv PATH $JRATE_GCC_HOME/bin:$PATH
#setenv PATH $JRATE_GCC_HOME/bin:$PATH
setenv LD_LIBRARY_PATH $JRATE_GCC_HOME/lib:$LD_LIBRARY_PATH

#setenv MANPATH $JRATE_GCC_HOME/man:$MANPATH
