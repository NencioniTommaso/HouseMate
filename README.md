# HouseMate backend server setup

1. Hardware & OS Preparation 
First, you must establish a stable physical environment for the server to run continuously without sleeping or crashing.

- Install the OS: Flash Ubuntu Server (for the laptop) or Raspberry Pi OS Lite (for the Pi).

- Prevent Sleep (Laptop Only): Edit /etc/systemd/logind.conf and change the lid switch setting to HandleLidSwitch=ignore so the server stays alive when closed.

- Storage Swap (Pi Only): Flash the OS onto a USB 3.0 SSD and boot from USB. Do not use a MicroSD card, or the database will eventually corrupt it.Static Local IP: 

- Log into your friends' router and assign the machine a static local IP (e.g., 192.168.1.50) so it never changes after a reboot.

2. Install Dependencies 
You only need to install one major piece of software: Docker. It will handle Java, Maven, and PostgreSQL for you.

Install Docker: Run the standard apt commands to install docker.io and docker-compose.

Configure Permissions: Add your user to the Docker group (sudo usermod -aG docker $USER) and run newgrp docker. This allows you to execute deployment commands without needing root access.

3. The Initial Deployment

Because of your IaC (Infrastructure as Code) setup, installing the actual application takes less than two minutes.

Clone the Code: Run git clone to pull down your repository, which safely includes your Dockerfile and docker-compose.yml. 

Inject the Secrets: Create a new file named .env in the root of the cloned repository (since it is safely ignored by .gitignore). Add your JWT_SECRET, DB_USER, and DB_PASSWORD.

Launch the Server: Run docker compose up -d. Docker will automatically download Java 21, compile your application, pull the Postgres database, link them together, and start them in the background.

4. Pushing Future Updates

When you fix a bug on your personal laptop, pushing that update to the hardware is a simple, two-step process that causes zero downtime for the database.

Pull Code: SSH into the server and run git pull to fetch your latest commits.

Rebuild and Restart: Run docker compose up -d --build. Docker will securely recompile your Java .jar, swap out the old backend container for the new one, and leave your database completely untouched.
