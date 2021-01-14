FROM python:3.8-slim

RUN apt-get update \
#    && apt-get install --yes python-dev python-pip libxml2-dev libxslt1-dev zlib1g-dev libffi-dev libssl-dev \
#    && apt-get install --yes libxml2-dev libxslt1-dev zlib1g-dev libffi-dev libssl-dev \
#    && apt-get install --yes build-essential \
   && apt-get -y install apt-transport-https ca-certificates curl gnupg2 software-properties-common \
   && apt-get clean \
   && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
RUN add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable"

COPY requirements.txt /app/requirements.txt
WORKDIR /app

RUN pip install --upgrade pip
RUN pip install -r requirements.txt
COPY app /app
CMD ["echo", "Runner is ready"']
