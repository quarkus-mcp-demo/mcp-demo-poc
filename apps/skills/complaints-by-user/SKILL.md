---
name: complaints-by-user
description: Get full text of all complaints raised by a specific username
---

# Get Complaints by Username

Retrieve and display the full text of all complaints raised by the user specified in {{args}}.

## Instructions:

1. Query the complaints database for all complaints where the username matches {{args}}
2. Display only the list of issue description and nothing else. 

3. If no username is provided in {{args}}, ask the user for the username

4. If no complaints are found for the username, inform the user clearly

Format the output so each complaint is easy to read and distinguish from others.
