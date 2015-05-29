#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
psql -p $PGPORT project < $DIR/../src/triggers.sql
psql -p $PGPORT project < $DIR/../src/create_tables.sql
psql -p $PGPORT project < $DIR/../src/create_index.sql
psql -p $PGPORT project < $DIR/../src/load_data.sql
