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
	FOR tempI IN SELECT connectionid FROM connection_usr WHERE userid = connName AND status = 'Accept' OR connectionid = connName AND status = 'Accept' LOOP
		SELECT * INTO tempResult FROM connection_usr WHERE userid = tempI.userid AND connectionid = reqName AND status = 'Accept' OR userid = reqName AND connectionid = tempI.userid AND status = 'Accept';
		IF NOT FOUND THEN
			FOR tempJ IN SELECT connectionid FROM connection_usr WHERE userid = tempI.userid AND connectionid <> connName AND status = 'Accept' OR connectionid = tempI.userid AND userid <> connName AND status = 'Accept' LOOP 
				SELECT * INTO tempResult FROM connection_usr WHERE userid = tempJ.userid AND connectionid = reqName AND status = 'Accept' OR userid = reqName AND connectionid = tempJ.userid AND status = 'Accept';
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
