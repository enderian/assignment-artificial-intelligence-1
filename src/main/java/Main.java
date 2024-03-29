/*
    Artificial Intelligence Assignment 1 - Ion Androutsopoulos
    Authored by:
        p3150007 Vasileiou Ismini
        p3150133 Pagkalos Spyridon
 */
import calc.HeuristicCalculator;
import model.Class;
import model.Lesson;
import reader.LessonReader;
import reader.TeacherReader;
import state.Classroom;
import state.Hour;
import state.Schedule;
import state.ScheduleComparator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    public final static int DEPTH_COST = 10;

    public static void main(String[] args) {

        Schedule.LESSONS = LessonReader.readLessons(new File("lessons.csv"));
        Schedule.TEACHERS = TeacherReader.readTeachers(new File("teachers.csv"));
        Schedule.CLASSROOMS = new ArrayList<>();

        for (Class clazz : Class.values()) {
            for (int i = 1; i <= 3; i++) {
                Schedule.CLASSROOMS.add(new Classroom(clazz, i));
            }
        }
        Schedule.HOURS = new ArrayList<>();
        for (int day = 1; day <= 5; day++) {
            for (int hour = 1; hour <= 7; hour++) {
                Schedule.HOURS.add(new Hour(day, hour));
            }
        }

        for (Class clazz : Class.values()) {
            clazz.setHoursNeededPerWeek(Schedule.LESSONS.stream().filter(lesson -> lesson.getaClass() == clazz).map(Lesson::getHoursPerWeek).reduce((a1, a2) -> a1 + a2).orElse(0));
        }

        for (Lesson lesson : Schedule.LESSONS) {
            if (Schedule.TEACHERS.stream().noneMatch(it -> it.getTeachableLessons().contains(lesson.getId()))) {
                System.err.println("Nobody can teach lesson " + lesson.getId() + " " + lesson.getName() + "!");
            }
        }

        System.out.println("Available lessons: " + Schedule.LESSONS.size());
        System.out.println("Available teachers: " + Schedule.TEACHERS.size());

        int iterations = 0;
        long timeNow = System.currentTimeMillis();
        System.out.println("Started calculations at: " + timeNow);

        Schedule schedule = new Schedule(0);
        schedule.generateRandom();

        PriorityQueue<Schedule> a = new PriorityQueue<>(new ScheduleComparator());
        HashSet<Schedule> visited = new HashSet<>();
        a.offer(schedule);

        while (!a.isEmpty()) {
            Schedule current = a.poll();

            for (Schedule successor : current.generateChildren()) {
                if (!visited.contains(successor)) {
                    successor.distance = HeuristicCalculator.calculate(successor);
                    if (successor.distance == 0) {
                        //TODO GOAL
                        System.out.println(successor.toString());

                        try {
                            FileWriter fileWriter = new FileWriter("result.txt");
                            fileWriter.write(successor.toString() + "\n");
                            fileWriter.close();

                            System.out.println("Results written at result.txt");
                        } catch (IOException e) {
                            System.err.println("Failed to write to disk!");
                        }
                        //successor.distance += HeuristicCalculator.calculateLight(successor);

                        System.out.println("Ended at: " + System.currentTimeMillis());
                        System.out.println("This only took: " + (System.currentTimeMillis() - timeNow) / 1000 + "s");
                        System.out.println("and " + iterations + " iterations.");
                        return;
                    }
                    //successor.distance += HeuristicCalculator.calculateLight(successor);
                    successor.cost = current.cost + DEPTH_COST;
                    successor.priority = successor.cost + successor.distance;

                    a.offer(successor);
                    visited.add(successor);
                }
            }
            visited.add(current);
            System.out.println("Priority now: " + current.priority + " (" + a.size() + ", " + current.getDepth() + ")");
            iterations++;
        }
    }

}
