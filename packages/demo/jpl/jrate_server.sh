export JRATE_IMMORTAL_MEMORY_SIZE=300
export JRATE_HEAP_MEMORY_SIZE=200
export JRATE_METHOD_MEMORY_SIZE=500
export JRATE_SCOPES_ARENA_SIZE=300
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/yuez/RTZen/lib/
source /home/jay/newjrate/jRate/script/jRate-env.sh
./server -pm sd -lp 2 -hp 98


