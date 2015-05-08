
## Build

    mvn clean install

## Examples

    # Get help
    java -cp  ./target/myartifcat-1.0.0.jar  au.org.emii.PostgresEditor  -help
    usage: Updater
     -all          do all records
     -help         show help
     -p <arg>      password
     -stdout       dump with context fields to stdout
     -stdout2      dump to stdout
     -t <arg>      xslt file for transform
     -u <arg>      user
     -update       actually update the record in db
     -url <arg>    jdbc connection string, eg.
                   jdbc:postgresql://127.0.0.1/geonetwork
     -uuid <arg>   metadata record uuid

    
    # Transform and dump to stdout
    java -cp  ./target/myartifcat-1.0.0.jar  au.org.emii.PostgresEditor  -url jdbc:postgresql://127.0.0.1:5432/geonetwork -u geonetwork -p geonetwork -all -t scripts/test1.xslt -stdout 2>&1 | less



## example how to copy db

    ssh catalogue-123.aodn.org.au
    sudo -u postgres pg_dump -Fc geonetwork > geonetwork.dump

    # local
    rsync -avzP catalogue-123.aodn.org.au ... geonetwork.dump .
    ---
    sudo /etc/init.d/tomcat7_geonetwork_123  stop

    sudo -u postgres psql  -c 'drop database geonetwork '
    sudo -u postgres pg_restore /vagrant/geonetwork.dump  -C  -d postgres

    # or if production,
    sudo -u postgres pg_restore  -O -x ./geonetwork.dump  -C  -d postgres
    # 


