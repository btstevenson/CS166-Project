/* creates new msgID when user sends message */
CREATE SEQUENCE msgIDSequence;
SELECT setval('msgIDSequence', 27811);

/* searches for valid connection */
CREATE OR REPLACE FUNCTION conn_search(connName varchar, reqName varchar) RETURNS boolean AS $$
DECLARE
	tempResult connection_usr%rowtype;
	tempI connection_usr%rowtype;
	tempJ connection_usr%rowtype;
BEGIN
	FOR tempI IN SELECT connectionid FROM connection_usr WHERE userid = connName AND status = 'Accept' UNION SELECT userid FROM connection_usr WHERE connectionid = connName AND status = 'Accept' LOOP
		RAISE NOTICE 'tempI is: %', tempI.userid;
		SELECT * INTO tempResult FROM connection_usr WHERE userid = tempI.userid AND connectionid = reqName AND status = 'Accept' UNION SELECT * FROM connection_usr WHERE userid = reqName AND connectionid = tempI.userid AND status = 'Accept';
		IF NOT FOUND THEN
			RAISE NOTICE 'in second not found';
			FOR tempJ IN SELECT connectionid FROM connection_usr WHERE userid = tempI.userid AND connectionid <> connName AND status = 'Accept' UNION SELECT userid FROM connection_usr WHERE connectionid = tempI.userid AND userid <> connName AND status = 'Accept' LOOP
				RAISE NOTICE 'tempJ is: %', tempJ.userid; 
				SELECT * INTO tempResult FROM connection_usr WHERE userid = tempJ.userid AND connectionid = reqName AND status = 'Accept' UNION SELECT * FROM connection_usr WHERE userid = reqName AND connectionid = tempJ.userid AND status = 'Accept';
				IF FOUND THEN
					return true;
				END IF;
			END LOOP;
		ELSE
			return true;
		END IF;
	END LOOP;
	return false;
END
$$ LANGUAGE plpgsql;	
