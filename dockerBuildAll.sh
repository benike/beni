gnome-terminal --maximize \
--tab -e "bash -c 'cd gateway/; mvn clean package dockerfile:build; exec bash'" \
--tab -e "bash -c 'cd oauth2/; mvn clean package dockerfile:build; exec bash'" \
--tab -e "bash -c 'cd welcome/; mvn clean package dockerfile:build; exec bash'" \
--tab -e "bash -c 'cd amusement-park-ui/; mvn clean package dockerfile:build; exec bash'" \
--tab -e "bash -c 'cd zoo-ui/; mvn clean package dockerfile:build; exec bash'" \
--tab -e "bash -c 'cd amusement-park-micro/; mvn clean package dockerfile:build; exec bash'" \
--tab -e "bash -c 'cd zoo/; mvn clean package dockerfile:build; exec bash'"