# Recrec

Reconcile transactions between Vantiv and an integrator like Traction Rec.

## Build

```bash
brew install jenv
jenv enable-plugin export # enable setting JAVA_HOME
brew install openjdk@21
jenv add /usr/local/opt/openjdk@21
jenv local 21
./gradlew build
```

## Run

To run against the production endpoint run the build commands, and then run the binary in `build/distributions`

To run against the staging endpoint you can run via `./gradlew run`

### Modes

* Query by record ID
    * In this mode all transactions with the matching ReferenceNumber are retrieved (for Traction Rec this is the 18 character Salesforce ID of the corresponding transaction)
    * Input CSV will have two columns: Merchant, Id
    * Merchant is the Merchant ID AKA the Acceptor ID (if reconciling Traction Rec scheduled billing this is the org-wide billing MID)
* Query by vantiv ID
* Query by setup ID
* Query payment accoutns by token
* Query BIN by payment token
