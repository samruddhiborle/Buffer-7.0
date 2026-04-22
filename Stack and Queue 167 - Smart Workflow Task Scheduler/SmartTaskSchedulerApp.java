import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class SmartTaskSchedulerApp {
    private final Scanner input = new Scanner(System.in);
    private final Scheduler scheduler = new Scheduler();

    // Minimal login store using HashMap (username -> password/role/employeeId)
    private final Map<String, String> passwords = new HashMap<>();
    private final Map<String, String> roles = new HashMap<>();
    private final Map<String, String> userToEmployee = new HashMap<>();

    public static void main(String[] args) {
        new SmartTaskSchedulerApp().run();
    }

    SmartTaskSchedulerApp() {
        seedUsers();
        seedEmployees();
        seedSampleTasks();
        scheduler.reschedule("Initial scheduling");
    }

    private void run() {
        System.out.println("==== Smart Workflow Task Scheduler (DSA Edition) ====");
        while (true) {
            System.out.println("\nLogin as admin/employee (or type exit)");
            System.out.print("Username: ");
            String username = input.nextLine().trim();
            if ("exit".equalsIgnoreCase(username)) {
                System.out.println("Exiting...");
                return;
            }
            System.out.print("Password: ");
            String password = input.nextLine().trim();

            if (!authenticate(username, password)) {
                System.out.println("Invalid credentials.");
                continue;
            }

            String role = roles.get(username);
            if ("ADMIN".equals(role)) {
                adminMenu();
            } else {
                employeeMenu(userToEmployee.get(username));
            }
        }
    }

    private void adminMenu() {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add task");
            System.out.println("2. Assign (pin) task to employee");
            System.out.println("3. Update task priority");
            System.out.println("4. View all tasks");
            System.out.println("5. View schedule");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    addTaskFlow();
                    break;
                case "2":
                    assignTaskFlow();
                    break;
                case "3":
                    updatePriorityFlow();
                    break;
                case "4":
                    scheduler.printAllTasks();
                    break;
                case "5":
                    scheduler.printSchedule();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void employeeMenu(String employeeId) {
        while (true) {
            System.out.println("\n--- Employee Menu (" + employeeId + ") ---");
            System.out.println("1. View my tasks");
            System.out.println("2. Mark task completed");
            System.out.println("3. Report issue");
            System.out.println("4. View schedule");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    scheduler.printTasksForEmployee(employeeId);
                    break;
                case "2":
                    completeTaskFlow(employeeId);
                    break;
                case "3":
                    reportIssueFlow(employeeId);
                    break;
                case "4":
                    scheduler.printSchedule();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private boolean authenticate(String username, String password) {
        return passwords.containsKey(username) && passwords.get(username).equals(password);
    }

    private void addTaskFlow() {
        try {
            System.out.print("Task ID: ");
            String id = input.nextLine().trim();
            System.out.print("Task name: ");
            String name = input.nextLine().trim();
            System.out.print("Priority (HIGH/MEDIUM/LOW): ");
            Task.Priority priority = Task.Priority.fromString(input.nextLine().trim());
            System.out.print("Deadline (yyyy-MM-dd HH:mm): ");
            LocalDateTime deadline = LocalDateTime.parse(input.nextLine().trim(), Scheduler.TIME_FORMAT);
            System.out.print("Duration hours: ");
            int duration = Integer.parseInt(input.nextLine().trim());
            System.out.print("Dependencies task IDs (comma separated, optional): ");
            String depRaw = input.nextLine().trim();

            List<String> deps = new ArrayList<>();
            if (!depRaw.isBlank()) {
                deps = Arrays.asList(depRaw.split("\\s*,\\s*"));
            }

            Task task = new Task(id, name, priority, deadline, duration, deps);
            if (!scheduler.addTask(task)) {
                System.out.println("Task ID already exists.");
                return;
            }
            scheduler.reschedule("New task added: " + id);
        } catch (Exception exception) {
            System.out.println("Invalid input: " + exception.getMessage());
        }
    }

    private void assignTaskFlow() {
        System.out.print("Task ID: ");
        String taskId = input.nextLine().trim();
        System.out.print("Employee ID (E1/E2/E3): ");
        String employeeId = input.nextLine().trim().toUpperCase();
        scheduler.pinTask(taskId, employeeId);
        scheduler.reschedule("Admin pinned task " + taskId + " to " + employeeId);
    }

    private void updatePriorityFlow() {
        System.out.print("Task ID: ");
        String taskId = input.nextLine().trim();
        System.out.print("New Priority (HIGH/MEDIUM/LOW): ");
        Task.Priority priority = Task.Priority.fromString(input.nextLine().trim());
        scheduler.updatePriority(taskId, priority);
        scheduler.reschedule("Priority changed for task " + taskId);
    }

    private void completeTaskFlow(String employeeId) {
        System.out.print("Task ID to complete: ");
        String taskId = input.nextLine().trim();
        if (!scheduler.isTaskAssignedToEmployee(taskId, employeeId)) {
            System.out.println("You can complete only your assigned tasks.");
            return;
        }
        scheduler.markCompleted(taskId);
        scheduler.reschedule("Task completed: " + taskId);
    }

    private void reportIssueFlow(String employeeId) {
        System.out.print("Task ID with issue: ");
        String taskId = input.nextLine().trim();
        if (!scheduler.isTaskAssignedToEmployee(taskId, employeeId)) {
            System.out.println("You can report issue only for your assigned tasks.");
            return;
        }
        System.out.print("Issue note: ");
        String note = input.nextLine().trim();
        System.out.print("Extra delay hours: ");
        int delay = Integer.parseInt(input.nextLine().trim());
        scheduler.reportIssue(taskId, note, delay);
        scheduler.reschedule("Issue reported for task " + taskId);
    }

    private void seedUsers() {
        passwords.put("admin", "admin123");
        roles.put("admin", "ADMIN");

        passwords.put("john", "emp123");
        roles.put("john", "EMPLOYEE");
        userToEmployee.put("john", "E1");

        passwords.put("sarah", "emp123");
        roles.put("sarah", "EMPLOYEE");
        userToEmployee.put("sarah", "E2");

        passwords.put("mike", "emp123");
        roles.put("mike", "EMPLOYEE");
        userToEmployee.put("mike", "E3");
    }

    private void seedEmployees() {
        scheduler.addEmployee(new Employee("E1", "John"));
        scheduler.addEmployee(new Employee("E2", "Sarah"));
        scheduler.addEmployee(new Employee("E3", "Mike"));
    }

    private void seedSampleTasks() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        scheduler.addTask(new Task("T1", "Requirement Analysis", Task.Priority.HIGH, now.plusHours(8), 2, List.of()));
        scheduler.addTask(new Task("T2", "Database Design", Task.Priority.HIGH, now.plusHours(16), 4, List.of("T1")));
        scheduler.addTask(new Task("T3", "API Implementation", Task.Priority.MEDIUM, now.plusHours(20), 5, List.of("T2")));
        scheduler.addTask(new Task("T4", "UI Build", Task.Priority.MEDIUM, now.plusHours(18), 4, List.of("T1")));
    }

    static class Task {
        enum Priority {
            HIGH(3), MEDIUM(2), LOW(1);

            final int weight;

            Priority(int weight) {
                this.weight = weight;
            }

            static Priority fromString(String value) {
                for (Priority p : values()) {
                    if (p.name().equalsIgnoreCase(value)) {
                        return p;
                    }
                }
                return MEDIUM;
            }
        }

        final String id;
        final String name;
        Priority priority;
        LocalDateTime deadline;
        final int durationHours;
        final List<String> dependencies;
        String status = "PENDING"; // PENDING, ASSIGNED, COMPLETED, ISSUE
        String issueNote = "";
        int delayHours = 0;
        String assignedEmployeeId;
        LocalDateTime startTime;
        LocalDateTime endTime;

        Task(String id, String name, Priority priority, LocalDateTime deadline, int durationHours, List<String> deps) {
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.deadline = deadline;
            this.durationHours = durationHours;
            this.dependencies = new ArrayList<>(deps);
        }

        int effectiveDuration() {
            return durationHours + Math.max(0, delayHours);
        }
    }

    static class Employee {
        final String id;
        final String name;
        LocalDateTime availableAt;
        int assignedHours;
        final List<String> assignedTaskIds = new ArrayList<>();

        Employee(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class Scheduler {
        static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // HashMaps used for core storage
        private final Map<String, Task> tasks = new HashMap<>();
        private final Map<String, Employee> employees = new HashMap<>();
        private final Map<String, String> pinnedAssignments = new HashMap<>();
        private final Map<String, String> taskToEmployee = new HashMap<>();

        boolean addTask(Task task) {
            if (tasks.containsKey(task.id)) {
                return false;
            }
            tasks.put(task.id, task);
            return true;
        }

        void addEmployee(Employee employee) {
            employees.put(employee.id, employee);
        }

        void pinTask(String taskId, String employeeId) {
            if (tasks.containsKey(taskId) && employees.containsKey(employeeId)) {
                pinnedAssignments.put(taskId, employeeId);
            } else {
                System.out.println("Invalid task or employee.");
            }
        }

        void updatePriority(String taskId, Task.Priority priority) {
            Task task = tasks.get(taskId);
            if (task != null) {
                task.priority = priority;
            } else {
                System.out.println("Task not found.");
            }
        }

        void markCompleted(String taskId) {
            Task task = tasks.get(taskId);
            if (task == null) {
                System.out.println("Task not found.");
                return;
            }
            task.status = "COMPLETED";
            task.endTime = LocalDateTime.now().withSecond(0).withNano(0);
            task.delayHours = 0;
            task.issueNote = "";
        }

        void reportIssue(String taskId, String note, int extraDelayHours) {
            Task task = tasks.get(taskId);
            if (task == null) {
                System.out.println("Task not found.");
                return;
            }
            task.status = "ISSUE";
            task.issueNote = note;
            task.delayHours += Math.max(1, extraDelayHours);
            task.priority = Task.Priority.HIGH;
        }

        boolean isTaskAssignedToEmployee(String taskId, String employeeId) {
            Task task = tasks.get(taskId);
            return task != null && employeeId.equals(task.assignedEmployeeId) && !"COMPLETED".equals(task.status);
        }

        void reschedule(String reason) {
            LocalDateTime startPoint = LocalDateTime.now().withSecond(0).withNano(0);
            resetEmployees(startPoint);
            taskToEmployee.clear();

            Map<String, List<String>> graph = new HashMap<>();
            Map<String, Integer> indegree = new HashMap<>();
            for (Task task : tasks.values()) {
                if (!"COMPLETED".equals(task.status)) {
                    graph.put(task.id, new ArrayList<>());
                    indegree.put(task.id, 0);
                    task.assignedEmployeeId = null;
                    task.startTime = null;
                    task.endTime = null;
                }
            }

            for (Task task : tasks.values()) {
                if (!indegree.containsKey(task.id)) {
                    continue;
                }
                for (String depId : task.dependencies) {
                    Task dep = tasks.get(depId);
                    if (dep != null && !"COMPLETED".equals(dep.status) && indegree.containsKey(depId)) {
                        graph.get(depId).add(task.id);
                        indegree.put(task.id, indegree.get(task.id) + 1);
                    }
                }
            }

            // PriorityQueue for scheduling: highest priority, then earliest deadline.
            PriorityQueue<Task> readyTasks = new PriorityQueue<>(Comparator
                    .comparingInt((Task t) -> t.priority.weight).reversed()
                    .thenComparing(t -> t.deadline)
                    .thenComparing(t -> t.id));

            // Kahn's Algorithm: add nodes with indegree = 0.
            Queue<String> topoCheckQueue = new LinkedList<>();
            for (Map.Entry<String, Integer> e : indegree.entrySet()) {
                if (e.getValue() == 0) {
                    topoCheckQueue.offer(e.getKey());
                    readyTasks.offer(tasks.get(e.getKey()));
                }
            }

            Map<String, Integer> tempIndegree = new HashMap<>(indegree);
            int topoVisited = 0;
            while (!topoCheckQueue.isEmpty()) {
                String current = topoCheckQueue.poll();
                topoVisited++;
                for (String next : graph.get(current)) {
                    tempIndegree.put(next, tempIndegree.get(next) - 1);
                    if (tempIndegree.get(next) == 0) {
                        topoCheckQueue.offer(next);
                    }
                }
            }
            if (topoVisited != indegree.size()) {
                System.out.println("Cycle detected in dependencies. Fix dependencies first.");
                return;
            }

            // Greedy assignment: always pick least-loaded employee (min-heap).
            PriorityQueue<Employee> leastLoadedEmployees = new PriorityQueue<>(Comparator
                    .comparingInt((Employee e) -> e.assignedHours)
                    .thenComparing(e -> e.availableAt)
                    .thenComparing(e -> e.id));
            leastLoadedEmployees.addAll(employees.values());

            Map<String, LocalDateTime> finishTime = new HashMap<>();
            int scheduledCount = 0;
            while (!readyTasks.isEmpty() && !leastLoadedEmployees.isEmpty()) {
                Task task = readyTasks.poll();
                Employee employee = chooseEmployee(task, leastLoadedEmployees);

                LocalDateTime depReadyTime = startPoint;
                for (String depId : task.dependencies) {
                    LocalDateTime depFinish = finishTime.get(depId);
                    if (depFinish != null && depFinish.isAfter(depReadyTime)) {
                        depReadyTime = depFinish;
                    }
                }

                LocalDateTime actualStart = employee.availableAt.isAfter(depReadyTime) ? employee.availableAt : depReadyTime;
                LocalDateTime actualEnd = actualStart.plusHours(task.effectiveDuration());

                task.assignedEmployeeId = employee.id;
                task.startTime = actualStart;
                task.endTime = actualEnd;
                task.status = "ISSUE".equals(task.status) ? "ISSUE" : "ASSIGNED";
                taskToEmployee.put(task.id, employee.id);
                finishTime.put(task.id, actualEnd);

                employee.availableAt = actualEnd;
                employee.assignedHours += task.effectiveDuration();
                employee.assignedTaskIds.add(task.id);
                leastLoadedEmployees.offer(employee);

                scheduledCount++;
                for (String next : graph.get(task.id)) {
                    indegree.put(next, indegree.get(next) - 1);
                    if (indegree.get(next) == 0) {
                        readyTasks.offer(tasks.get(next));
                    }
                }
            }

            if (scheduledCount != graph.size()) {
                System.out.println("Warning: some tasks could not be scheduled.");
            }

            System.out.println("\nSchedule updated: " + reason);
            printSchedule();
        }

        private void resetEmployees(LocalDateTime startPoint) {
            for (Employee e : employees.values()) {
                e.availableAt = startPoint;
                e.assignedHours = 0;
                e.assignedTaskIds.clear();
            }
        }

        private Employee chooseEmployee(Task task, PriorityQueue<Employee> minHeap) {
            String pinnedEmployee = pinnedAssignments.get(task.id);
            if (pinnedEmployee == null) {
                return minHeap.poll();
            }
            List<Employee> buffer = new ArrayList<>();
            Employee chosen = null;
            while (!minHeap.isEmpty()) {
                Employee e = minHeap.poll();
                if (e.id.equals(pinnedEmployee)) {
                    chosen = e;
                    break;
                }
                buffer.add(e);
            }
            minHeap.addAll(buffer);
            return chosen != null ? chosen : minHeap.poll();
        }

        void printSchedule() {
            List<Task> ordered = new ArrayList<>();
            for (Task task : tasks.values()) {
                if (!"COMPLETED".equals(task.status) && task.startTime != null) {
                    ordered.add(task);
                }
            }
            ordered.sort(Comparator.comparing(t -> t.startTime));

            System.out.println("\nTask -> Employee -> Start Time");
            if (ordered.isEmpty()) {
                System.out.println("No active scheduled tasks.");
                return;
            }
            for (Task t : ordered) {
                System.out.printf("%s (%s) -> %s -> %s%n",
                        t.id, t.priority, t.assignedEmployeeId, t.startTime.format(TIME_FORMAT));
            }
        }

        void printAllTasks() {
            List<String> ids = new ArrayList<>(tasks.keySet());
            Collections.sort(ids);
            System.out.println("\nAll Tasks:");
            for (String id : ids) {
                Task t = tasks.get(id);
                System.out.printf("%s | %s | %s | status=%s | assigned=%s | deps=%s%n",
                        t.id, t.name, t.priority, t.status,
                        t.assignedEmployeeId == null ? "-" : t.assignedEmployeeId,
                        t.dependencies);
            }
        }

        void printTasksForEmployee(String employeeId) {
            List<Task> mine = new ArrayList<>();
            for (Task t : tasks.values()) {
                if (employeeId.equals(t.assignedEmployeeId)) {
                    mine.add(t);
                }
            }
            mine.sort(Comparator.comparing(t -> t.deadline));
            System.out.println("\nMy Tasks:");
            if (mine.isEmpty()) {
                System.out.println("No assigned tasks.");
                return;
            }
            for (Task t : mine) {
                System.out.printf("%s | %s | %s | status=%s | deadline=%s | issue=%s%n",
                        t.id, t.name, t.priority, t.status,
                        t.deadline.format(TIME_FORMAT),
                        t.issueNote.isBlank() ? "-" : t.issueNote);
            }
        }
    }
}
