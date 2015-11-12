# ppid3

Implementation of the "Privacy preserving ID3 using Gini Index over horizontally partitioned data" as described by S. Samet and A. Miri.

## Project Structure
Implementations of the two subprotocols: "Multi-party Addition" and "Multi-party Multiplication" can be found in the [computations package](core/src/main/java/de/henku/computations). See the Javadoc of [SecureComputationMaster.java](core/src/main/java/de/henku/computations/SecureComputationMaster.java) for detailed usage information.

The "Secure multi-party square division" protocol implementation and the ID3 itself can be found in [id3_horizontal package](core/src/main/java/de/henku/algorithm/id3_horizontal).

Examples can be found in module: [examples](example/src/main/java/de/henku/example/id3).

## Current State
The current implementation only works with two involved parties. If more parties are involved and the runtime should not grow exponentially, there is a need to share a private key. Otherwise, the final result could not be properly decrypted.

An alternative solution to replace the private key exchange is the usage of a custom key-pair on each party. When using the Paillier cryptographic system the size of this key-pair has to be big enough to hold all possible encrypted values from the previous parties. Unfortunately this leads to an exponentially growing runtime and much overhead for organizing the different key-pairs.

## Dependencies
https://github.com/kunerd/jpaillier

## References
 * Samet, S.; Miri, A., "Privacy preserving ID3 using Gini Index over horizontally partitioned data," in Computer Systems and Applications, 2008. AICCSA 2008. IEEE/ACS International Conference on , vol., no., pp.645-651, March 31 2008-April 4 2008
 http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=4493598&isnumber=4493499
