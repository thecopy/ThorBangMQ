class AddTimestampToLogEntry < ActiveRecord::Migration
  def change
    add_column :log_entries, :timestamp, :integer
  end
end
