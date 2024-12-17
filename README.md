<h1>Soup Clock</h1>
This is a custom clock developed as a student project for use at a high school. Anyone who wants to use it should be aware that the remote email functionality will not work without replacing the credentials in MailCheckRunnable.java and recompiling the code, as the credentials currently listed in the code do not work. 

Key features:
<ul>
  <li>Several customizable parameters</li>
  <li>Festive themes for certain occasions</li>
  <li>Supports custom themes</li>
  <li>Optionally can display the day's schedule and highlight the current period</li>
  <li>Optionally can play animations at the end of periods in the schedule</li>
  <li>Optionally can display the time and date digitally</li>
  <li>Optional remote control via email</li>
</ul>

Note: If the credentials in MailCheckRunnable.java are updated, those credentials should not be pushed to Git, as it is a security risk. If this clock is to be used again, it would be good to update it such that it can pull those credentials from a configuration file.
