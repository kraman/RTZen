cp OVMServer_NAT/ior.txt OVMClient_NAT/ior.txt
cd OVMClient_NAT
./ovm img perf.jitter.Client
cp timeRecords.txt ../.
cd ..

