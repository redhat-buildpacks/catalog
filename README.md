## RHTAP Templates

This project contains the code source needed to feed a RHTAP runtime demo project like also Tekton PipelineAsCode

To use them, perform the following actions using the github cli:

- Git auth
`echo $(PASSWORD_STORE_DIR=~/.password-store-work pass show github/apps/dabou-1/github_token) | gh auth login --with-token`

- Create github project

```bash
ORG_NAME=halkyonio
REPO_TEMPLATE=rhtap-templates
REPO_DEMO_NAME=rhtap-buildpack-demo-1
REPO_DEMO_TITLE="RHTAP Buildpack Demo 1"

gh repo delete $ORG_NAME/$REPO_DEMO_NAME --yes
gh repo create $ORG_NAME/$REPO_DEMO_NAME --public

rm -rf $REPO_DEMO_NAME; mkdir $REPO_DEMO_NAME; cd $REPO_DEMO_NAME
git init
echo "## RHTAP Buildpack Demo 1" > README.md
git add .

## Import runtime code
BRANCH=main
wget https://github.com/$ORG_NAME/$REPO_TEMPLATE/archive/$BRANCH.zip
unzip $BRANCH.zip
mv $REPO_TEMPLATE-$BRANCH/quarkus-hello/.mvn/ .
mv $REPO_TEMPLATE-$BRANCH/quarkus-hello/* .

echo "$REPO_TEMPLATE-$BRANCH/" > .gitignore
echo "$BRANCH.zip" >> .gitignore
echo "target/" >> .gitignore

git add .
git commit -m "Upload quarkus hello runtime"

git branch -M main
git remote add origin git@github.com:$ORG_NAME/$REPO_DEMO_NAME.git
git push -u origin main
cd ..
```
- Test it locally
```bash
cd $REPO_DEMO_NAME
mvn clean compile; mvn quarkus:dev

http :8080/hello
http :8080/hello/greeting/charles
cd ..
```
- Creating a .Tekton project
```bash
cd $REPO_DEMO_NAME
mkdir .tekton
mv $REPO_TEMPLATE-$BRANCH/tekton/template-push.yaml .tekton/$REPO_DEMO_NAME-push.yaml
git add .tekton/$REPO_DEMO_NAME-push.yaml
```

- Setting the RHTAP parameters
```bash
APPLICATION_NAME=$REPO_DEMO_NAME
COMPONENT_NAME="quarkus-hello"
PAC_NAME=$COMPONENT_NAME-$(openssl rand -base64 4 | tr -dc 'a-zA-Z0-9' | cut -c1-5)
PAC_YAML_FILE=".tekton/$REPO_DEMO_NAME-push.yaml"
# REGISTRY_URL=quay.io/redhat-user-workloads/cmoullia-tenant/$REPO_DEMO_NAME
REGISTRY_URL=quay.io/$ORG_NAME/$REPO_DEMO_NAME

sed -i.bak "s/#APPLICATION_NAME#/$APPLICATION_NAME/g" $PAC_YAML_FILE
sed -i.bak "s/#COMPONENT_NAME#/"$COMPONENT_NAME"/g" $PAC_YAML_FILE
sed -i.bak "s/#PAC_NAME#/"$PAC_NAME"/g" $PAC_YAML_FILE
sed -i.bak "s/#REGISTRY_URL#/"$REGISTRY_URL"/g"  $PAC_YAML_FILE
rm $PAC_YAML_FILE.bak
git commit -sm "Add the tekton push file" .tekton/$REPO_DEMO_NAME-push.yaml; git push
cd ..
```

- Cleaning
```bash
rm $BRANCH.zip; rm -r $REPO_TEMPLATE-$BRANCH
```



