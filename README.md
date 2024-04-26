# pandora

pandora is a naive implementation of distributed file storage in Java.
pandora leverages the [Content-Addressable Storage (CAS)](https://en.wikipedia.org/wiki/Content-addressable_storage) approach to 
uniquely identify and store files. Each file is hashed, and the hash 
is used as its address, ensuring efficient retrieval and storage across 
distributed nodes.
